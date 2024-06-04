package common;

public enum RequestCode
{
    Connected("1"),
    Login("2"),
    Success("200"),
    Failure("400"),
    Pending("3"),
    Register("500"),
    Encrypt("5"),
    Decrypt("6"),
    NoMessages("707");

    private final String code;

    RequestCode(String code) {this.code = code;}

    public String getCode() {return this.code;}
}
