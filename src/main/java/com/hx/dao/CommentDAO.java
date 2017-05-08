package com.hx.dao;

import com.hx.model.Comment;
import org.apache.ibatis.annotations.*;
import java.util.List;


@Mapper
public interface CommentDAO {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status , parent , count ,to_userid";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status},#{parent},#{count},#{toUserId})"})
    int addComment(Comment comment);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Comment getCommentById(int id);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where entity_id=#{entityId} and entity_type=#{entityType} and parent=0 order by created_date desc limit #{offset},#{limit}"})
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId,
                                        @Param("entityType") int entityType,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    @Select({"select count(id) from ", TABLE_NAME, " where entity_id=#{entityId} and entity_type=#{entityType} and parent = 0"})
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);


    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where parent=#{parent}"})
    List<Comment> getCommentChild(@Param("parent") int parent);

    @Update({"update comment set status=#{status} where id=#{id}"})
    int updateStatus(@Param("id") int id, @Param("status") int status);

    @Delete({"delete from" , TABLE_NAME ," where id=#{id}"})
    int deleteComment(@Param("id") int id);

    @Delete({"delete from" , TABLE_NAME ," where entity_id=#{id}"})
    int deleteCommentbyQuestion(@Param("id") int id);

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    int getUserCommentCount(int userId);

    @Update({"update comment set content=#{content},created_date=#{createdDate} where id=#{id}"})
    int updateComment(Comment comment);


    @Update({"update comment set count=#{count} where id=#{parent}"})
    int updateCommentCount(Comment comment);
}
