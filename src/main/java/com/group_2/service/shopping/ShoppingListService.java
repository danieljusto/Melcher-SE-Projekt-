package com.group_2.service.shopping;

import com.group_2.dto.shopping.ShoppingListDTO;
import com.group_2.dto.shopping.ShoppingListItemDTO;
import com.group_2.dto.shopping.ShoppingMapper;
import com.group_2.dto.core.CoreMapper;
import com.group_2.dto.core.UserSummaryDTO;
import com.group_2.model.User;
import com.group_2.model.shopping.ShoppingList;
import com.group_2.model.shopping.ShoppingListItem;
import com.group_2.repository.UserRepository;
import com.group_2.repository.shopping.ShoppingListItemRepository;
import com.group_2.repository.shopping.ShoppingListRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Service for shopping lists and their items
@Service
@Transactional
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ShoppingMapper shoppingMapper;
    private final CoreMapper coreMapper;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingListItemRepository itemRepository,
            UserRepository userRepository, ShoppingMapper shoppingMapper, CoreMapper coreMapper) {
        this.shoppingListRepository = shoppingListRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.shoppingMapper = shoppingMapper;
        this.coreMapper = coreMapper;
    }

    public List<UserSummaryDTO> getMemberSummaries(Long wgId) {
        if (wgId == null) {
            return List.of();
        }
        return coreMapper.toUserSummaries(userRepository.findByWgId(wgId));
    }

    public ShoppingList createList(String name, User creator, List<User> sharedWith) {
        ShoppingList list = new ShoppingList(name, creator, sharedWith);
        return shoppingListRepository.save(list);
    }

    public ShoppingList createPrivateList(String name, User creator) {
        return createList(name, creator, null);
    }

    public List<ShoppingList> getAccessibleLists(User user) {
        return shoppingListRepository.findAllAccessibleByUser(user);
    }

    public Optional<ShoppingList> getList(Long id) {
        return shoppingListRepository.findById(id);
    }

    public ShoppingList shareList(ShoppingList list, List<User> users) {
        list.setSharedWith(users);
        return shoppingListRepository.save(list);
    }

    public ShoppingList addSharedUser(ShoppingList list, User user) {
        list.addSharedUser(user);
        return shoppingListRepository.save(list);
    }

    public ShoppingList removeSharedUser(ShoppingList list, User user) {
        list.removeSharedUser(user);
        return shoppingListRepository.save(list);
    }

    public void deleteList(ShoppingList list) {
        shoppingListRepository.delete(list);
    }

    public ShoppingListItem addItem(ShoppingList list, String itemName, User creator) {
        ShoppingListItem item = new ShoppingListItem(itemName, creator, list);
        return itemRepository.save(item);
    }

    public void removeItem(ShoppingListItem item) {
        itemRepository.delete(item);
    }

    public List<ShoppingListItem> getItemsForList(ShoppingList list) {
        return itemRepository.findByShoppingList(list);
    }

    public ShoppingListItem updateItem(ShoppingListItem item, String newName) {
        item.setName(newName);
        return itemRepository.save(item);
    }

    public ShoppingListItem toggleBought(ShoppingListItem item) {
        item.setBought(!Boolean.TRUE.equals(item.getBought()));
        return itemRepository.save(item);
    }

    // ========== DTO Methods ==========

    public List<ShoppingListDTO> getAccessibleListsDTO(User user) {
        return shoppingMapper.toDTOList(getAccessibleLists(user));
    }

    public List<ShoppingListDTO> getAccessibleListsDTO(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userRepository.findById(userId).map(this::getAccessibleListsDTO).orElseGet(List::of);
    }

    public Optional<ShoppingListDTO> getListDTO(Long id) {
        return getList(id).map(shoppingMapper::toDTO);
    }

    public List<ShoppingListItemDTO> getItemsForListDTO(Long listId) {
        Optional<ShoppingList> list = getList(listId);
        if (list.isEmpty()) {
            return List.of();
        }
        return shoppingMapper.toItemDTOList(getItemsForList(list.get()));
    }

    public ShoppingListDTO createListByUserIds(String name, Long creatorId, List<Long> sharedWithIds) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        List<User> sharedWith = new java.util.ArrayList<>();
        if (sharedWithIds != null) {
            for (Long userId : sharedWithIds) {
                userRepository.findById(userId).ifPresent(sharedWith::add);
            }
        }

        ShoppingList list = createList(name, creator, sharedWith);
        return shoppingMapper.toDTO(list);
    }

    public ShoppingListItemDTO addItemByIds(Long listId, String itemName, Long creatorId) {
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list not found"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        ShoppingListItem item = addItem(list, itemName, creator);
        return shoppingMapper.toItemDTO(item);
    }

    public ShoppingListItemDTO toggleBoughtById(Long itemId) {
        ShoppingListItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        ShoppingListItem updated = toggleBought(item);
        return shoppingMapper.toItemDTO(updated);
    }

    public void removeItemById(Long itemId) {
        ShoppingListItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        removeItem(item);
    }

    public void deleteListById(Long listId) {
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list not found"));
        deleteList(list);
    }

    public ShoppingListDTO shareListByIds(Long listId, List<Long> sharedWithIds) {
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list not found"));

        List<User> sharedWith = new java.util.ArrayList<>();
        if (sharedWithIds != null) {
            for (Long userId : sharedWithIds) {
                userRepository.findById(userId).ifPresent(sharedWith::add);
            }
        }

        ShoppingList updated = shareList(list, sharedWith);
        return shoppingMapper.toDTO(updated);
    }

    // Called when WG member leaves - deletes their lists and removes them from
    // shared lists
    public void cleanupListsForDepartingUser(Long userId) {
        if (userId == null) {
            return;
        }

        // 1. Delete all lists created by this user
        List<ShoppingList> createdLists = shoppingListRepository.findByCreatorId(userId);
        for (ShoppingList list : createdLists) {
            shoppingListRepository.delete(list);
        }

        // 2. Remove user from all lists they were shared with (using ID-based query)
        List<ShoppingList> sharedLists = shoppingListRepository.findBySharedWithUserId(userId);
        for (ShoppingList list : sharedLists) {
            // Use removeIf with ID comparison for reliability
            list.getSharedWith(); // Force load if lazy
            shoppingListRepository.findById(list.getId()).ifPresent(managedList -> {
                // Get the actual internal list and modify it
                User userToRemove = userRepository.findById(userId).orElse(null);
                if (userToRemove != null) {
                    managedList.removeSharedUser(userToRemove);
                    shoppingListRepository.save(managedList);
                }
            });
        }
    }

    /**
     * Remove a user from all shopping lists where they are in the sharedWith list.
     * This is called when a WG member leaves to clean up their access to shared
     * lists. Note: Lists created by this user are not deleted; they remain for the
     * user.
     * 
     * @deprecated Use cleanupListsForDepartingUser instead for complete cleanup
     */
    @Deprecated
    public void removeUserFromAllSharedLists(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        cleanupListsForDepartingUser(user.getId());
    }

    /**
     * Remove a user from all shopping lists by user ID. This is called when a WG
     * member leaves to clean up their access to shared lists.
     * 
     * @deprecated Use cleanupListsForDepartingUser instead for complete cleanup
     */
    @Deprecated
    public void removeUserFromAllSharedLists(Long userId) {
        cleanupListsForDepartingUser(userId);
    }
}
