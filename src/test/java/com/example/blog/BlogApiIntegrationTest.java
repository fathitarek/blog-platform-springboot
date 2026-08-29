package com.example.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.comment.repository.CommentRepository;
import com.example.blog.post.domain.Category;
import com.example.blog.post.domain.PostEntity;
import com.example.blog.post.repository.PostRepository;
import com.example.blog.user.domain.RoleEntity;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.domain.UserEntity;
import com.example.blog.user.repository.RoleRepository;
import com.example.blog.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerLoginAndCreatePostFlowWorks() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.user.roles[0]").value("AUTHOR"));

        String token = loginAndGetToken("alice@example.com", "password123");

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Boot Basics","content":"Content","category":"TECHNOLOGY"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring Boot Basics"))
                .andExpect(jsonPath("$.author.email").value("alice@example.com"));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Basics"));
    }

    @Test
    void authorCannotUpdateAnotherAuthorsPostButAdminCan() throws Exception {
        String aliceToken = registerAndLogin("Alice", "alice@example.com", "password123");
        String bobToken = registerAndLogin("Bob", "bob@example.com", "password123");
        createAdminUser();
        String adminToken = loginAndGetToken("admin@example.com", "Admin1234!");

        MvcResult postResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Alice Post","content":"Original","category":"LIFESTYLE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Long postId = readId(postResult);

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Bob tries","content":"Nope","category":"EDUCATION"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Admin updated","content":"Updated","category":"EDUCATION"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin updated"));
    }

    @Test
    void commentsFilteringPaginationAndUnauthorizedAccessWork() throws Exception {
        String aliceToken = registerAndLogin("Alice", "alice@example.com", "password123");
        String bobToken = registerAndLogin("Bob", "bob@example.com", "password123");
        createAdminUser();
        String adminToken = loginAndGetToken("admin@example.com", "Admin1234!");

        Long springPostId = createPost(aliceToken, "Spring Data", "Spring content", Category.TECHNOLOGY);
        Long lifePostId = createPost(bobToken, "Healthy Habits", "Life content", Category.LIFESTYLE);
        Long eduPostId = createPost(adminToken, "Study Tips", "Edu content", Category.EDUCATION);

        mockMvc.perform(post("/api/posts/{postId}/comments", springPostId)
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Great article"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great article"));

        mockMvc.perform(get("/api/posts")
                        .param("category", "TECHNOLOGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Data"));

        mockMvc.perform(get("/api/posts")
                        .param("authorId", String.valueOf(findUserIdByEmail("bob@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Healthy Habits"));

        mockMvc.perform(get("/api/posts")
                        .param("fromDate", LocalDate.now().toString())
                        .param("toDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/posts")
                        .param("search", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "title")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"No auth","content":"Denied","category":"TECHNOLOGY"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/posts/{id}/comments", springPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great article"));

        mockMvc.perform(delete("/api/comments/{id}", findCommentIdByPostId(springPostId)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/comments/{id}", findCommentIdByPostId(springPostId))
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(eduPostId)).isTrue();
        assertThat(postRepository.existsById(lifePostId)).isTrue();
    }

    private String registerAndLogin(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterPayload(name, email, password))))
                .andExpect(status().isCreated());
        return loginAndGetToken(email, password);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private void createAdminUser() {
        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
        userRepository.save(new UserEntity(
                "Admin",
                "admin@example.com",
                passwordEncoder.encode("Admin1234!"),
                Set.of(adminRole)
        ));
    }

    private Long createPost(String token, String title, String content, Category category) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","content":"%s","category":"%s"}
                                """.formatted(title, content, category.name())))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private Long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private Long findCommentIdByPostId(Long postId) {
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId).get(0).getId();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record RegisterPayload(String name, String email, String password) {
    }

    private record LoginPayload(String email, String password) {
    }
}
