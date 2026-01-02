package com.group_2.service.shopping;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.shopping.ShoppingList;
import com.group_2.model.shopping.ShoppingListItem;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShoppingListServiceTest {

    @Autowired
    private ShoppingListService shoppingListService;

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
    void createsPrivateList() {
        // When
        ShoppingList list = shoppingListService.createPrivateList("Groceries", creator);

        // Then
        assertThat(list.getId()).isNotNull();
        assertThat(list.getName()).isEqualTo("Groceries");
        assertThat(list.getCreator().getEmail()).isEqualTo("creator@example.com");
        assertThat(list.isShared()).isFalse();
    }

    @Test
    void createsSharedList() {
        // When
        ShoppingList list = shoppingListService.createList("Shared Groceries", creator, List.of(sharedUser));

        // Then
        assertThat(list.getId()).isNotNull();
        assertThat(list.getName()).isEqualTo("Shared Groceries");
        assertThat(list.isShared()).isTrue();
        assertThat(list.getSharedWith()).hasSize(1);
    }

    @Test
    void getsAccessibleLists() {
        // Given
        shoppingListService.createPrivateList("My List", creator);
        ShoppingList sharedList = shoppingListService.createList("Shared", sharedUser, List.of(creator));
        shoppingListService.createPrivateList("Other's List", sharedUser);

        // When
        List<ShoppingList> accessible = shoppingListService.getAccessibleLists(creator);

        // Then
        assertThat(accessible).hasSize(2);
        assertThat(accessible).extracting(ShoppingList::getName)
                .containsExactlyInAnyOrder("My List", "Shared");
    }

    @Test
    void addsItemToList() {
        // Given
        ShoppingList list = shoppingListService.createPrivateList("Groceries", creator);

        // When
        ShoppingListItem item = shoppingListService.addItem(list, "Milk", creator);

        // Then
        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo("Milk");
        assertThat(item.getBought()).isFalse();
    }

    @Test
    void togglesBoughtStatus() {
        // Given
        ShoppingList list = shoppingListService.createPrivateList("Groceries", creator);
        ShoppingListItem item = shoppingListService.addItem(list, "Milk", creator);
        assertThat(item.getBought()).isFalse();

        // When
        ShoppingListItem toggled = shoppingListService.toggleBought(item);

        // Then
        assertThat(toggled.getBought()).isTrue();
    }

    @Test
    void removesItem() {
        // Given
        ShoppingList list = shoppingListService.createPrivateList("Groceries", creator);
        ShoppingListItem item = shoppingListService.addItem(list, "Milk", creator);
        Long itemId = item.getId();

        // When
        shoppingListService.removeItem(item);

        // Then
        List<ShoppingListItem> items = shoppingListService.getItemsForList(list);
        assertThat(items).isEmpty();
    }

    @Test
    void deletesListWithItems() {
        // Given
        ShoppingList list = shoppingListService.createPrivateList("Groceries", creator);
        shoppingListService.addItem(list, "Milk", creator);
        shoppingListService.addItem(list, "Bread", creator);
        Long listId = list.getId();

        // When
        shoppingListService.deleteList(list);

        // Then
        Optional<ShoppingList> found = shoppingListService.getList(listId);
        assertThat(found).isEmpty();
    }

    @Test
    void addsSharedUser() {
        // Given
        ShoppingList list = shoppingListService.createPrivateList("My List", creator);
        assertThat(list.isShared()).isFalse();

        // When
        shoppingListService.addSharedUser(list, sharedUser);

        // Then
        Optional<ShoppingList> found = shoppingListService.getList(list.getId());
        assertThat(found.get().isShared()).isTrue();
        assertThat(found.get().getSharedWith()).hasSize(1);
    }

    @Test
    void removesSharedUser() {
        // Given
        ShoppingList list = shoppingListService.createList("Shared", creator, List.of(sharedUser));
        assertThat(list.isShared()).isTrue();

        // When
        shoppingListService.removeSharedUser(list, sharedUser);

        // Then
        Optional<ShoppingList> found = shoppingListService.getList(list.getId());
        assertThat(found.get().isShared()).isFalse();
    }
}
