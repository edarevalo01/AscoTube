package co.com.picolab;

public class Video {

    public int id;
    public String nombre;
    public String titulo;
    public String descripcion;
    public int posx;
    public int posy;
    public boolean frente;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    public boolean getFrente() {
        return frente;
    }

    public void setFrente(boolean frente) {
        this.frente = frente;
    }

    @Override
    public String toString() {
        return nombre + " " + titulo + " \n" + descripcion + " \n" + posx + " " + posy + "\n\n";
    }
}
