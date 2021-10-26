package app.family.family;

public class getuser {
    String fname, sname;

    public getuser() {
    }

    public getuser(String fname, String sname) {
        this.fname = fname;
        this.sname = sname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }
}
