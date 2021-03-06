package com.tagdish.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tagdish.constant.TagDishServiceConstant;
import com.tagdish.exception.TagDishServiceException;
import com.tagdish.service.INotificationService;

@Controller
@RequestMapping("/notification")
public class NotificationController extends CommonController {
	
	private static final Logger logger = LoggerFactory
			.getLogger(NotificationController.class);		
	
    @Autowired
    private INotificationService notificationService;		
	
    
	@RequestMapping(value = "/notifyEvent", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String notifyEvent(@RequestBody final String notifyJson,
			@RequestParam(value = "isList", required = false) Boolean isList,
			Model model) {	
		logger.debug(" NotificationController .. notify()");
		
		String status = null;
		long start = System.currentTimeMillis();
		try {
			logger.debug("NotificationController .. notify .." + notifyJson);
			
			notificationService.notify(notifyJson, isList);
			status = TagDishServiceConstant.SUCCESS;
		} catch (TagDishServiceException e) {

			e.printStackTrace();
			logger.error("TagDishServiceException occured @ NotificationController.notify", e);
			status = TagDishServiceConstant.ERROR;
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ NotificationController.notify", e);
			status = TagDishServiceConstant.ERROR;
		}
		
		long end = System.currentTimeMillis();
		logger.info("NotificationController notify " + (end - start) + ",in ms");		

		return status;
	}	
}
