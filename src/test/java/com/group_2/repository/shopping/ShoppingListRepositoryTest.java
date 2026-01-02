package com.group_2.repository.shopping;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.shopping.ShoppingList;
import com.group_2.repository.UserRepository;
import com.group_2.repository.WGRepository;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShoppingListRepositoryTest {

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    private WG wg;
    private User creator;
    private User sharedUser;

    @BeforeEach
    void setUp() {
        wg = wgRepository.save(TestDataFactory.wg("Test WG"));
        creator = userRepository.save(TestDataFactory.user("creator@example.com", wg));
        sharedUser = userRepository.save(TestDataFactory.user("shared@example.com", wg));
    }

    @Test
    void savesAndRetrievesShoppingList() {
        // Given
        ShoppingList list = TestDataFactory.shoppingList("Groceries", creator);

        // When
        ShoppingList saved = shoppingListRepository.save(list);

        // Then
        assertThat(saved.getId()).isNotNull();
        ShoppingList found = shoppingListRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Groceries");
        assertThat(found.getCreator().getEmail()).isEqualTo("creator@example.com");
    }

    @Test
    void findsByCreator() {
        // Given
        ShoppingList list1 = shoppingListRepository.save(TestDataFactory.shoppingList("List 1", creator));
        ShoppingList list2 = shoppingListRepository.save(TestDataFactory.shoppingList("List 2", creator));
        shoppingListRepository.save(TestDataFactory.shoppingList("Other List", sharedUser));

        // When
        List<ShoppingList> found = shoppingListRepository.findByCreator(creator);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(ShoppingList::getName)
                .containsExactlyInAnyOrder("List 1", "List 2");
    }

    @Test
    void findsBySharedWithContaining() {
        // Given
        ShoppingList list = TestDataFactory.shoppingList("Shared List", creator);
        list.addSharedUser(sharedUser);
        shoppingListRepository.save(list);

        // When
        List<ShoppingList> found = shoppingListRepository.findBySharedWithContaining(sharedUser);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Shared List");
    }

    @Test
    void findsAllAccessibleByUser() {
        // Given
        ShoppingList ownedList = shoppingListRepository.save(TestDataFactory.shoppingList("Owned", creator));

        ShoppingList sharedList = TestDataFactory.shoppingList("Shared With Me", sharedUser);
        sharedList.addSharedUser(creator);
        shoppingListRepository.save(sharedList);

        shoppingListRepository.save(TestDataFactory.shoppingList("Not Accessible", sharedUser));

        // When
        List<ShoppingList> found = shoppingListRepository.findAllAccessibleByUser(creator);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(ShoppingList::getName)
                .containsExactlyInAnyOrder("Owned", "Shared With Me");
    }

    @Test
    void findsByCreatorId() {
        // Given
        shoppingListRepository.save(TestDataFactory.shoppingList("List 1", creator));
        shoppingListRepository.save(TestDataFactory.shoppingList("List 2", creator));

        // When
        List<ShoppingList> found = shoppingListRepository.findByCreatorId(creator.getId());

        // Then
        assertThat(found).hasSize(2);
    }
}
