package common;

public enum RequestCode
{
    Connected("PZghI62FeW"),
    Login("D7UMekhwXc"),
    Success("nshA25KR4p"),
    Failure("UtcTrrJGuI"),
    Pending("gPyoTBsz86"),
    Register("0t2ienbSEw"),
    Encrypt("5rQi6GL2Bl"),
    Decrypt("svoPjUkECe"),
    NoMessages("DwOcdgyTaO");

    private final String code;

    RequestCode(String code) {this.code = code;}

    public String getCode() {return this.code;}
}
