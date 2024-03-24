package com.under10s.user.helper;


import com.under10s.user.dao.entity.UserModel;
import com.under10s.user.dto.UserDetailsDTO;
import org.springframework.beans.BeanUtils;

public class ModelMapper {

    public static UserModel getUserModelFromRegisterDTO(UserDetailsDTO userDetailsDTO) {
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDetailsDTO, userModel);
        return userModel;
    }

    public static UserDetailsDTO getResponseUserDetailsDtoFromUserModel(UserModel user) {
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
        userDetailsDTO.setUserId(user.getUserId());
        userDetailsDTO.setEmailId(user.getEmailId());
        userDetailsDTO.setFirstName(user.getFirstName());
        userDetailsDTO.setLastName(user.getLastName());
        userDetailsDTO.setRoleId(user.getRoleModel().getRoleId().intValue());
        return userDetailsDTO;
    }
}
