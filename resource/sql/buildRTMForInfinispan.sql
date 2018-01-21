 
  create table init_rtm
   as select * from
   (
         select issue_id, summary, description, group_concat(file_path,"和") as file_path,
     group_concat(message," ") as message
     from
     (
        select code_change.commit_hash as commit_hash, file_path,message
        from code_change, change_set
        where code_change.commit_hash=change_set.commit_hash
        and (sum_added_lines - sum_removed_lines) >= 0 and file_path like '%java' and file_path not like '%Test.java'
      ) as commit_file,
      (
        select issue.issue_id, commit_hash,summary, description 
        from  issue, change_set_link
        where issue.issue_id=change_set_link.issue_id and issue_type in ('Feature Request') 
        and resolved_date is not null and priority in ('Major','Critical')
      ) as issue_commit
      where commit_file.commit_hash=issue_commit.commit_hash
      group by issue_id
   )
   
   
