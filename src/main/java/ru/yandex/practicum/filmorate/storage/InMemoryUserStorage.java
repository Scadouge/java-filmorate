package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage extends AbstractStorage<User> implements UserStorage {

    @Override
    public User put(User item) {
        Long id = item.getId();
        if (id == null) {
            id = generateId();
            item = item.toBuilder().id(id).build();
        }
        return super.put(item);
    }

    @Override
    public Collection<User> getFriends(Long id) {
        User user = storage.get(id);
        return user.getFriends().stream().map(storage::get).collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = storage.get(id);
        User other = storage.get(otherId);
        return user.getFriends().stream()
                .map(storage::get)
                .filter(fid -> other.getFriends().contains(fid.getId())).collect(Collectors.toList());
    }
}
