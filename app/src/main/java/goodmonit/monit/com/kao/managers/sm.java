package goodmonit.monit.com.kao.managers;

public class sm {
    public String getParameter(int type) {
        return gp(type);
    }

    private native String gp(int type);
    static {
        System.loadLibrary("server-lib");
    }
}