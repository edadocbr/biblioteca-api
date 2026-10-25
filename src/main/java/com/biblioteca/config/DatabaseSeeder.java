package com.biblioteca.config;

import com.biblioteca.entity.Bibliotecario;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.enums.EstadoEjemplar;
import com.biblioteca.entity.enums.EstadoUsuario;
import com.biblioteca.repository.BibliotecarioRepository;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner seedDatabase(
            LibroRepository libroRepository,
            EjemplarRepository ejemplarRepository,
            BibliotecarioRepository bibliotecarioRepository
    ) {
        return args -> {
            // 1. Crear Administrador (solo si no existe)
            if (!bibliotecarioRepository.existsByCodigoEmpleado("ADM-001")) {
            Bibliotecario admin = new Bibliotecario(
                "00000000",
                "admin",
                "admin@sgbu.com",
                EstadoUsuario.ACTIVO,
                "ADM-001",
                "Completo"
            );
            admin.setPassword("admin123");
            bibliotecarioRepository.save(admin);
            }

            // 2. Crear Libros
            List<Libro> libros = new ArrayList<>();
            libros.add(new Libro("978-84-123456-01-1", "Las Formas del Espacio", "Adolf Loos", "Gustavo Gili", 1931));
            libros.add(new Libro("978-84-123456-02-8", "Teoría de la Forma", "Johannes Itten", "Alianza Editorial", 1921));
            libros.add(new Libro("978-84-123456-03-5", "El Mundo como Voluntad y Representación", "Arthur Schopenhauer", "Ediciones Akal", 1819));
            libros.add(new Libro("978-84-123456-04-2", "Hacia una Arquitectura", "Le Corbusier", "Editorial Poseidón", 1923));
            libros.add(new Libro("978-84-123456-05-9", "La Tipografía Moderna", "Jan Tschichold", "Editorial Gustavo Gili", 1928));
            libros.add(new Libro("978-84-123456-06-6", "El Ensayo como Forma", "Theodor W. Adorno", "Ediciones Siruela", 1958));
            libros.add(new Libro("978-84-123456-07-3", "Espacio, Tiempo y Arquitectura", "Sigfried Giedion", "Doubleday", 1941));
            libros.add(new Libro("978-84-123456-08-0", "Notas sobre lo Obvio", "Vilém Flusser", "Siruela", 1993));
            libros.add(new Libro("978-84-123456-09-7", "Grid Systems in Graphic Design", "Josef Müller-Brockmann", "Niggli Verlag", 1981));
            libros.add(new Libro("978-84-123456-10-3", "La Genealogía de la Moral", "Friedrich Nietzsche", "Alianza Editorial", 1887));

            for (Libro libro : libros) {
                if (!libroRepository.existsById(libro.getIsbn())) {
                    libroRepository.save(libro);
                }
            }

            // 3. Crear Ejemplares en estado DISPONIBLE
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000101", "A1B2C3D4", "EST-01-A", EstadoEjemplar.DISPONIBLE, libros.get(0)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000102", "A1B2C3D5", "EST-01-B", EstadoEjemplar.DISPONIBLE, libros.get(1)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000103", "A1B2C3D6", "EST-01-C", EstadoEjemplar.DISPONIBLE, libros.get(2)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000104", "A1B2C3D7", "EST-01-D", EstadoEjemplar.DISPONIBLE, libros.get(3)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000105", "A1B2C3D8", "EST-02-A", EstadoEjemplar.DISPONIBLE, libros.get(4)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000106", "A1B2C3D9", "EST-02-B", EstadoEjemplar.DISPONIBLE, libros.get(5)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000107", "A1B2C3DA", "EST-02-C", EstadoEjemplar.DISPONIBLE, libros.get(6)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000108", "A1B2C3DB", "EST-02-D", EstadoEjemplar.DISPONIBLE, libros.get(7)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000109", "A1B2C3DC", "EST-03-A", EstadoEjemplar.DISPONIBLE, libros.get(8)));
            saveEjemplarIfAbsent(ejemplarRepository, new Ejemplar("BC-000110", "A1B2C3DD", "EST-03-B", EstadoEjemplar.DISPONIBLE, libros.get(9)));
        };
    }

    private static void saveEjemplarIfAbsent(EjemplarRepository repo, Ejemplar ejemplar) {
        if (!repo.existsById(ejemplar.getCodigoBarra())) {
            repo.save(ejemplar);
        }
    }
}
