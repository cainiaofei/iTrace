package cn.edu.nju.cs.itrace4.parser.javatext;

/**
 * Created by niejia on 15/2/22.
 */
public class JField {
    private String fieldName;
    private String typeName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String toString() {
        return typeName + "::" + fieldName;
    }
}
