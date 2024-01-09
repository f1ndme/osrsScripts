package QuickMine.resources;

public class Functions {
    public static int cantorPairing(int x,int y) {
        return (x+y) * (x+y+1) / 2+y; //generate unique int from x,y coords.
    }


}
