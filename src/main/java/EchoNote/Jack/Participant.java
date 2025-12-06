package EchoNote.Jack;

public class Participant {
    private final String name;
    private final String email;
    private final String role;

    public Participant(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
