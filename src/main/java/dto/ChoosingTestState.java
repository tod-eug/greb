package dto;

import java.util.List;
import java.util.Map;

public class ChoosingTestState {

    private final Map<String, List<Test>> categories;

    public ChoosingTestState(Map<String, List<Test>> categories) {
        this.categories = categories;
    }

    public Map<String, List<Test>> getCategories() {
        return categories;
    }
}
