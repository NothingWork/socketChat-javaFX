package org.example.bean;

/**
 * @author Yun
 * @description: 包含role和content的类
 */
public class RoleContent {
    private String role;
    private String content;

    public RoleContent(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
