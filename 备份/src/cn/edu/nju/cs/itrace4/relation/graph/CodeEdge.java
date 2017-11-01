package cn.edu.nju.cs.itrace4.relation.graph;

/**
 * Created by niejia on 15/1/6.
 */
public class CodeEdge implements Comparable<CodeEdge>{
    private final Integer id;
    private EdgeType type;
    private CodeVertex source;
    private CodeVertex target;
    private double score;

    public CodeEdge(Integer id, EdgeType type) {
        this.id = id;
        this.type = type;
    }

    public CodeEdge(Integer id, EdgeType type, CodeVertex source, CodeVertex target) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edge:");
        sb.append(String.valueOf(id));
        sb.append(" source:" + getSource());
        sb.append(" target:" + getTarget());
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object y) {
        if (y == this) return true;
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        CodeEdge that = (CodeEdge) y;
        if (this.id != that.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + id.hashCode();
        return hash;
    }

    public EdgeType getType() {
        return type;
    }

    public CodeVertex getSource() {
        return source;
    }

    public CodeVertex getTarget() {
        return target;
    }

    public int getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(CodeEdge that) {
        if (this.score > that.score) return +1;
        if (this.score < that.score) return -1;
        return 0;
    }
}
