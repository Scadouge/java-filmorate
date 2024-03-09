package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService extends AbstractService<UserStorage, User> {
    public UserService(UserStorage storage) {
        super(storage);
    }

    public User updateFriends(Long id, Long friendId, boolean isDeletion) {
        User user = getItemOrThrow(id);
        User friend = getItemOrThrow(friendId);
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
        User user = getItemOrThrow(id);
        return storage.getFriends(user.getId());
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = getItemOrThrow(id);
        User other = getItemOrThrow(otherId);
        return storage.getCommonFriends(user.getId(), other.getId());
    }
}
