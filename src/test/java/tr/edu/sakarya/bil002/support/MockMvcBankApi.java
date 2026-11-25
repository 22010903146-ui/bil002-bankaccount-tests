package tr.edu.sakarya.bil002.support;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public final class MockMvcBankApi {
    private MockMvcBankApi() { }
    public static BankApi create(MockMvc mvc) {
        return new BankApi((method, path, body) -> {
            try {
                var request = MockMvcRequestBuilders.request(HttpMethod.valueOf(method), path)
                        .accept(MediaType.APPLICATION_JSON);
                if (body != null) {
                    request.contentType(MediaType.APPLICATION_JSON).content(body);
                }
                var response = mvc.perform(request).andReturn().getResponse();
                String text = response.getContentAsString();
                var json = text.isBlank() ? com.fasterxml.jackson.databind.node.NullNode.getInstance()
                        : BankApi.JSON.readTree(text);
                return new BankApi.Reply(response.getStatus(), json);
            } catch (Exception e) {
                throw new AssertionError("MockMvc request failed", e);
            }
        });
    }
}
