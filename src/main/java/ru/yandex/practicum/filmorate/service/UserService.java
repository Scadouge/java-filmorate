package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService extends AbstractService<UserStorage, User> {
    public UserService(UserStorage storage) {
        super(storage);
    }

    public User updateFriends(Long id, Long friendId, boolean isDeletion) {
        log.info("Обновление списка друзей id={}, friendId={}, isDeletion={}", id, friendId, isDeletion);
        User user = getItem(id);
        User friend = getItem(friendId);
        User updatedUser = updateUserFriends(user, friend, isDeletion);
        User updatedFriend = updateUserFriends(friend, user, isDeletion);
        storage.put(updatedUser);
        storage.put(updatedFriend);
        return updatedUser;
    }

    private User updateUserFriends(User user, User friend, boolean isDeletion) {
        Set<Long> friends = new HashSet<>(user.getFriends());
        if (isDeletion) {
            friends.remove(friend.getId());
        } else {
            friends.add(friend.getId());
        }
        return user.toBuilder().friends(friends).build();
    }

    public Collection<User> getFriends(Long id) {
        log.info("Получение списка друзей id={}", id);
        User user = getItem(id);
        return storage.getFriends(user.getId());
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей id={}, otherId={}", id, otherId);
        Collection<User> friends = storage.getFriends(id);
        User other = getItem(otherId);
        return friends.stream().filter(friend -> other.getFriends().contains(friend.getId())).collect(Collectors.toList());
    }
}
