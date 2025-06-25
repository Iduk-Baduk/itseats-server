package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
}
