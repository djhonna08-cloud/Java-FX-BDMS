public class LambdaTest {
    public void setupHandlers() {
        button.setOnAction(e -> handleClick());
        list.forEach(item -> System.out.println(item));
    }
}
