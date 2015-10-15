package com.tagdish.service;

import com.tagdish.exception.TagDishServiceException;

public interface INotificationService {

	public abstract void notify(String notifyJson, Boolean isList) throws TagDishServiceException;
}