# Problemas con Scanner y Decimales en Java: Solución Universal

## Descripción
Al usar `Scanner.nextDouble()` en Java para leer números decimales desde archivos, el separador decimal depende del "locale" del sistema operativo:

- **Locale en español o países europeos:** La coma (`,`) se usa como separador decimal.
- **Locale en inglés (US) o internacional:** El punto (`.`) se usa como separador decimal.

Si tu archivo tiene decimales con punto (ejemplo: `-42.766285`) y tu sistema está configurado en español, `Scanner.nextDouble()` puede fallar y lanzar **InputMismatchException**.

## Solución Universal

Antes de leer los datos numéricos, configura el Scanner con el locale internacional:

```java
import java.util.Locale;
import java.util.Scanner;

Scanner scanner = new Scanner(new File("archivo.txt"));
scanner.useLocale(Locale.ROOT); // O Locale.US
```

De esta forma, **Java espera el punto como separador decimal** y tus datos se leen correctamente, sin importar el idioma de tu sistema operativo, teclado o país.

## Ejemplo completo

```java
Scanner scanner = new Scanner(new File("archivo.txt"));
scanner.useLocale(Locale.ROOT); // Solución universal para decimales

while (scanner.hasNextLine()) {
    String line = scanner.nextLine();
    String[] parts = line.split(";");
    double latitud = Double.parseDouble(parts[2].trim());
    double longitud = Double.parseDouble(parts[3].trim());
    // ... sigue el procesamiento
}
```
