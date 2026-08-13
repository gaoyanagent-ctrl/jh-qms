update qms_quality_characteristic
   set review_status = 'CONFIRMED',
       reviewed_by = coalesce(updated_by, created_by, 0),
       reviewed_at = current_timestamp,
       review_comment = 'AUTO_CONFIRMED_INSPECTION_DIMENSION',
       updated_at = current_timestamp,
       version = version + 1
 where review_status = 'PENDING'
   and inspection_dimension = true
   and reference_dimension = false
   and ideal_dimension = false
   and deleted = false;
