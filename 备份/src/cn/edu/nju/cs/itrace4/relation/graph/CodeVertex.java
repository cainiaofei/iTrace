package cn.edu.nju.cs.itrace4.relation.graph;

import java.io.Serializable;

/**
 * Created by niejia on 14/11/27.
 */
public class CodeVertex implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Integer id;
    private String name;

    public CodeVertex(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object y) {
        if (y == this) return true;
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        CodeVertex that = (CodeVertex) y;
        if (this.id != that.id) return false;
        if (!(this.name.equals(that.name))) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }
}
