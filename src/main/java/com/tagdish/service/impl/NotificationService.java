package com.tagdish.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tagdish.constant.TagDishServiceConstant;
import com.tagdish.dao.jdbc.NotificationDAO;
import com.tagdish.domain.db.NotificationDB;
import com.tagdish.domain.dto.NotificationDTO;
import com.tagdish.domain.dto.RestaurantDishDTO;
import com.tagdish.domain.dto.search.SearchResultDTO;
import com.tagdish.exception.TagDishServiceException;
import com.tagdish.service.INotificationService;

@Service
public class NotificationService extends CommonService implements INotificationService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(NotificationService.class);	
	
	@Autowired
	private Gson gson;	
	
	@Autowired
    private NotificationDAO notificationDAO;
		
	public void notify(String notifyJson, Boolean isList) throws TagDishServiceException {
		
		logger.debug("NotificationService - notify() - start");
		List<NotificationDTO> notificaitonList = null;
		NotificationDTO notificationDTO = null;
		if(isList != null && isList) {
			
			Type listType = new TypeToken<ArrayList<NotificationDTO>>() {}.getType();
			notificaitonList = gson.fromJson(notifyJson, listType);
			
			for (NotificationDTO notificationDTO2 : notificaitonList) {
				saveNotification(notificationDTO2);
			}
		} else {
			
			notificationDTO =  gson.fromJson(notifyJson, NotificationDTO.class);
			saveNotification(notificationDTO);
		}
		logger.debug("NotificationService - notify() - end");
	}
	
	private void saveNotification(NotificationDTO notificationDTO) {
		
		List<NotificationDB> notificationDBList = null;
		NotificationDB notificationDB = null;
		
		notificationDB = new NotificationDB();
		BeanUtils.copyProperties(notificationDTO, notificationDB);
		notificationDB.setData(getData(notificationDTO));
		
		notificationDBList = notificationDAO.getNotificationList(notificationDB);
		if(notificationDBList == null || notificationDBList.size() == 0) {
			
			notificationDAO.saveNotification(notificationDB);
		} else {
			
			notificationDB = notificationDBList.get(0);
			notificationDB.setCount(notificationDB.getCount() + 1);
			notificationDB.setTimestamp(notificationDTO.getTimestamp());
			notificationDAO.updateNotification(notificationDB);
		}		
	}

	private String getSearchResultString(NotificationDTO notificationDTO) {
		
		SearchResultDTO searchResultDTO = notificationDTO.getSearchResultDTO();
		StringBuffer searchResult = new StringBuffer();
		if(searchResultDTO != null &&
				searchResultDTO.getDishRestaurantList() != null &&
				searchResultDTO.getDishRestaurantList().size() > 0){
			for (RestaurantDishDTO restaurantDishDTO : searchResultDTO.getDishRestaurantList()) {
				searchResult.append(restaurantDishDTO.getDishDTO().getDishId() + ",");
			}
		}
		return searchResult.toString();
	}
	
	private String getData(NotificationDTO notificationDTO) {
		String data = null;
		
		if(notificationDTO.getAction() != null) {
			if(notificationDTO.getAction().equalsIgnoreCase(TagDishServiceConstant.CALL_RESTAURANT_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishServiceConstant.VIEW_RESTAURANT_DETAIL_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishServiceConstant.VIEW_DISH_DETAIL_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishServiceConstant.VIEW_DIRECTION_RESTAURANT_NOTIFY_TYPE)) {
				data = notificationDTO.getEntityId();
			} else if(notificationDTO.getAction().equalsIgnoreCase(TagDishServiceConstant.SEARCH_NOTIFY_TYPE)) {
				data = gson.toJson(notificationDTO.getSearchInputDTO());
			}			
		}
		
		return data;
	}	
}
