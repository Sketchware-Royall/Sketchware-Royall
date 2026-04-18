package pro.sketchware.activities.resourceseditor.components.models;

public class IntModel {
    private String intName;
    private String intValue;

    public IntModel(String intName, String intValue) {
        this.intName = intName;
        this.intValue = intValue;
    }

    public String getIntName() {
        return intName;
    }

    public void setIntName(String intName) {
        this.intName = intName;
    }

    public String getIntValue() {
        return intValue;
    }

    public void setIntValue(String intValue) {
        this.intValue = intValue;
    }
}