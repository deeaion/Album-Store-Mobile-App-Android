using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using AlbumStore.Application.Interfaces;
using AlbumStore.Common.Config;
using AlbumStore.Domain.Entities;
using Microsoft.IdentityModel.Tokens;

namespace AlbumStore.Application.Services
{
    public class JwtTokenService : IJwtTokenService
    {
        private readonly JwtConfig _jwtConfig;

        public JwtTokenService(JwtConfig jwtConfig)
        {
            _jwtConfig = jwtConfig;
        }

        public string CreateToken(ApplicationUser user, string[] roles)
        {
            SigningCredentials signingCredentials = GetSigningCredentials();
            List<Claim> claims = GetClaims(user, roles);
            JwtSecurityToken tokenOptions = GenerateTokenOptions(signingCredentials, claims);
            return new JwtSecurityTokenHandler().WriteToken(tokenOptions);
        }

        private SigningCredentials GetSigningCredentials()
        {
            byte[] key = Encoding.UTF8.GetBytes(_jwtConfig.Secret);
            SymmetricSecurityKey secret = new(key);
            return new SigningCredentials(secret, SecurityAlgorithms.HmacSha256);
        }

        private static List<Claim> GetClaims(ApplicationUser user, string[] roles)
        {
            List<Claim> claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, user.UserName),
                new Claim(ClaimTypes.NameIdentifier, user.Id)
            };

            foreach (string role in roles)
            {
                claims.Add(new Claim(ClaimTypes.Role, role));
            }

            return claims;
        }

        private JwtSecurityToken GenerateTokenOptions(SigningCredentials signingCredentials, List<Claim> claims)
        {
            JwtSecurityToken tokenOptions = new JwtSecurityToken(
                issuer: _jwtConfig.Issuer,
                audience: _jwtConfig.Audience,
                claims: claims,
                expires: DateTime.Now.AddMinutes(_jwtConfig.ExpiresIn),
                signingCredentials: signingCredentials
            );

            return tokenOptions;
        }
    }
}
