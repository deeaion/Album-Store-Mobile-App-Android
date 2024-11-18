namespace AlbumStore.Common.Config;

public class JwtConfig
{
    public string Secret { get; set; }
    public string Issuer { get; set; }
    public string Audience { get; set; }
    public double ExpiresIn { get; set; }
}