package model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UTModel implements Serializable {
    private String utName;
    private Map<String, List<String>> uts = new TreeMap<>();

    public UTModel() {
    }

    public UTModel(String utName, Map<String, List<String>> uts) {
        this.utName = utName;
        this.uts = uts;
    }

    public String getUtName() {
        return utName;
    }

    public void setUtName(String utName) {
        this.utName = utName;
    }

    public Map<String, List<String>> getUts() {
        return uts;
    }

    public void setUts(Map<String, List<String>> uts) {
        this.uts = uts;
    }

    @Override
    public String toString() {
        return "UTModel{" + "utName=" + utName + ", uts=" + uts + '}';
    }

}
