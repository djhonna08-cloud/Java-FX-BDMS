public class Outer {
    public void outerMethod() {
        System.out.println("outer");
    }
    
    class Inner {
        public void innerMethod() {
            System.out.println("inner");
        }
    }
}
