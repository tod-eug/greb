package dto;

import java.util.List;
import java.util.Map;

public class ChoosingTestState {

    private String category;
    private Map<String, List<Test>> categories;
    private String testName;

    public ChoosingTestState() {
        category = "";
        testName = "";
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Map<String, List<Test>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<Test>> categories) {
        this.categories = categories;
    }
}
