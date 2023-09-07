function like(btn, entityType, entityId, entityUserId){
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("i").text(data.likeCount);
                console.log(data.likeStatus);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    )
}