package com.autobots.automanager.base;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
final class Senhas {
 private Senhas() {}
 static String hash(String senha) {
  if(senha.isBlank()) return senha;
  try {
   byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
   var spec=new PBEKeySpec(senha.toCharArray(),salt,210000,256);
   byte[] hash=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); spec.clearPassword();
   return "pbkdf2-sha256$210000$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(hash);
  } catch(java.security.GeneralSecurityException ex) { throw new IllegalStateException(ex); }
 }
}
