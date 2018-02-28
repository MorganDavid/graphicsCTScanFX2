package sample;

public class Vector2D {
    private int x;
    private int y;

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = x;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Vector2D add(Vector2D v){
        return new Vector2D(v.getX()+x, v.getY()+y);
    }
    public Vector2D subtract(Vector2D v){
        return new Vector2D(x-v.getX(), y-v.getY());
    }
    public Vector2D multiplyScalar(int scalar){
        return new Vector2D(x*scalar,y*scalar);
    }
}
