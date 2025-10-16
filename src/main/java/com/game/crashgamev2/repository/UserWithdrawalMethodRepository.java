package com.game.crashgamev2.repository;

import com.game.crashgamev2.UserWithdrawalMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserWithdrawalMethodRepository extends JpaRepository<UserWithdrawalMethod, Integer> {
    List<UserWithdrawalMethod> findAllByUser_Id(Integer userId);
}