package app.family.family;

public class IpAdresses {
    String ip="192.168.137.1";
    String urlLogin="http://"+ip+"/Familylocalhost/login.php";
    String getdt="http://"+ip+"/Familylocalhost/getData.php";

    public String getIp() {
        return ip;
    }

    public String getUrlLogin() {
        return urlLogin;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUrlLogin(String urlLogin) {
        this.urlLogin = urlLogin;
    }

    public String getGetdt() {
        return getdt;
    }

    public void setGetdt(String getdt) {
        this.getdt = getdt;
    }
}
