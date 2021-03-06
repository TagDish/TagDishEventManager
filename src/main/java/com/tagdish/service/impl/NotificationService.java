package com.tagdish.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tagdish.constant.TagDishDomainConstant;
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
	
	@Value("${save.search.result}")
	private boolean saveSearchResult;
		
	@Async
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
			
			notificationDAO.createNotification(notificationDB);
		} else {
			
			notificationDB = notificationDBList.get(0);
			notificationDB.setCount(notificationDB.getCount() + 1);
			notificationDB.setTimestamp(notificationDTO.getTimestamp());
			notificationDAO.updateNotification(notificationDB);
		}
		
		if(TagDishDomainConstant.SEARCH_NOTIFY_TYPE.equalsIgnoreCase(notificationDTO.getAction())) {
			saveSearchResult(notificationDB, notificationDTO);	
		}
	}
	
	private void saveSearchResult(NotificationDB notificationDB, NotificationDTO notificationDTO) {
		
		String searchResult = null;
		List<NotificationDB> notificationDBList = null;
		if(saveSearchResult) {
			searchResult = getSearchResultString(notificationDTO);
			
			if(notificationDB.getNotificationId() == null) {
				notificationDBList = notificationDAO.getNotificationList(notificationDB);
				notificationDAO.createSearchResultLog(notificationDBList.get(0).getNotificationId(), searchResult);
			} else {
				notificationDAO.createSearchResultLog(notificationDB.getNotificationId(), searchResult);
			}
		}
	}

	private String getSearchResultString(NotificationDTO notificationDTO) {
		
		SearchResultDTO searchResultDTO = notificationDTO.getSearchResultDTO();
		StringBuffer searchResult = new StringBuffer();
		String resultString = null;
		if(searchResultDTO != null &&
				searchResultDTO.getDishRestaurantList() != null &&
				searchResultDTO.getDishRestaurantList().size() > 0){
			for (RestaurantDishDTO restaurantDishDTO : searchResultDTO.getDishRestaurantList()) {
				searchResult.append(restaurantDishDTO.getDishDTO().getDishId() + ",");
			}
		}
		if(searchResult.length() > 1000) {
			resultString = searchResult.toString().substring(0, 1000);
		} else {
			resultString = searchResult.toString();
		}
		return resultString;
	}
	
	private String getData(NotificationDTO notificationDTO) {
		String data = null;
		
		if(notificationDTO.getAction() != null) {
			if(notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.CALL_RESTAURANT_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.VIEW_RESTAURANT_DETAIL_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.VIEW_DISH_DETAIL_NOTIFY_TYPE)
					|| notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.VIEW_DIRECTION_RESTAURANT_NOTIFY_TYPE)) {
				data = notificationDTO.getEntityId();
			} else if(notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.SEARCH_NOTIFY_TYPE) ||
					notificationDTO.getAction().equalsIgnoreCase(TagDishDomainConstant.MORE_RESULTS_NOTIFY_TYPE)) {
				data = gson.toJson(notificationDTO.getSearchInputDTO());
			}			
		}
		
		return data;
	}	
}
