package com.hx.service;

import com.hx.async.EventType;
import com.hx.dao.FeedDAO;
import com.hx.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FeedService {
    @Autowired
    FeedDAO feedDAO;

    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, EventType eventType,int offset, int limit) {
        return feedDAO.selectUserFeeds(maxId, userIds, eventType.getValue(),offset, limit);
    }

    public int getPullFeedsCount(List<Integer> userIds, EventType eventType){
        return feedDAO.getFeedsCount(userIds, eventType.getValue());
    }

    public boolean addFeed(Feed feed) {
        feedDAO.addFeed(feed);
        return feed.getId() > 0;
    }

    public Feed getById(int id) {
        return feedDAO.getFeedById(id);
    }
}
