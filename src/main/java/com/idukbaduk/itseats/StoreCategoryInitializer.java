package com.idukbaduk.itseats;

import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreCategoryInitializer implements CommandLineRunner {

    private static final String[][] CATEGORIES = {
            { "한식", "korean" },
            { "중식", "chinese" },
            { "피자", "pizza" },
            { "찜/탕", "stew" },
            { "치킨", "chicken" },
            { "분식", "bunsik" },
            { "돈까스", "porkcutlet" },
            { "족발/보쌈", "pigfeet" },
            { "구이", "grill" },
            { "일식", "japanese" },
            { "회/해물", "seafood" },
            { "양식", "western" },
            { "커피/차", "cafe" },
            { "디저트", "dessert" },
            { "간식", "snack" },
            { "아시안", "asian" },
            { "샌드위치", "sandwich" },
            { "샐러드", "salad" },
            { "버거", "burger" },
            { "멕시칸", "mexican" },
            { "도시락", "lunchbox" },
            { "죽", "porridge" },
            { "포장", "takeout" },
            { "1인분", "single" }
    };

    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        long count = storeCategoryRepository.count();

        if (count < CATEGORIES.length) {
            insertStoreCategory();
        }
    }

    private void insertStoreCategory() {
        List<StoreCategory> storeCategories = new ArrayList<>();
        for (String[] category : CATEGORIES) {
            StoreCategory storeCategory = StoreCategory.builder()
                    .categoryName(category[0])
                    .categoryCode(category[1])
                    .build();
            storeCategories.add(storeCategory);
        }
        storeCategoryRepository.saveAll(storeCategories);
    }
}
