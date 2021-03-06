xquery version "1.0";

module namespace density = "http://density.tlos.com/";

import module namespace hs="http://hs.tlos.com/" at "moduleReportOperations.xquery"; 
import module namespace met = "http://meta.tlos.com/" at "moduleMetaDataOperations.xquery";

declare namespace rep="http://www.likyateknoloji.com/XML_report_types";
declare namespace dat="http://www.likyateknoloji.com/XML_data_types";
declare namespace state-types="http://www.likyateknoloji.com/state-types";

(:
Mappings
$dailyScenariosDocumentUrl = doc("//db/TLOSSW/xmls/tlosSWDailyScenarios10.xml")
$sequenceDocumentUrl = doc("//db/TLOSSW/xmls/tlosSWSequenceData10.xml")
:)

(:
Programmed by : Hakan Saribiyik
Version : 0.1
First Release Date : 6 May 2013
UpdateDate :
Purpose : Job state density reporting.
Usage : 

 recStat( status, substatus, state, reportDateA, reportDateB, step)

example;
let $startDateTime := xs:dateTime("2013-05-05T15:42:00+03:00")
let $endDateTime   := xs:dateTime("2013-05-05T15:59:00+03:00")

return  density:recStat(xs:string("RUNNING"), xs:string("ON-RESOURCE"), xs:string("TIME-IN") , $startDateTime, $endDateTime, xs:dayTimeDuration('PT60S'))

:)

declare function density:stringToDateTime($t1 as xs:string) as xs:dateTime
{
    (: Degisik tarih formatlari icin dusunuldu. 2011-10-13T15:08:31+0300 veya 2011-10-13T15:08:31.91+0300 veya 2011-10-13T15:08:31.897+0300 :)
    
    let $t2 := substring-before($t1, '+')
    let $t3 := substring-after($t1, '+')
    let $t7 := concat($t2, '+', substring($t3,1,2) , ':00')
    let $t8 := xs:dateTime($t7)
    
    return $t8
};

(:
let $startDateTime := xs:dateTime("2013-05-04T16:15:00+03:00")
let $endDateTime   := xs:dateTime("2013-06-01T00:00:00+03:00")
let $stateName := xs:string("FINISHED")
let $substateName := xs:string("COMPLETED")
let $statusName := xs:string("SUCCESS")
:)
declare function density:focusedRecords($documentUrl as xs:string, $startDateTime as xs:dateTime, $endDateTime as xs:dateTime, $reportParameters as element(rep:reportParameters) ) as node()
{
    let $getJobs := hs:getJobsReport($documentUrl, $reportParameters)

    let $sonuc := 
              for $jobProperties in $getJobs/dat:jobProperties
              let $liveStateInfos := $jobProperties/dat:stateInfos/state-types:LiveStateInfos
              let $liveStateInfosSorted := <state-types:LiveStateInfos> {
                                       for $item in $liveStateInfos/state-types:LiveStateInfo
                                       let $lSIDateTime := density:stringToDateTime($item/@LSIDateTime)
                                       order by $lSIDateTime ascending
                                       return $item
              }</state-types:LiveStateInfos>
              let $fish :=  <rep:group agentId="{$jobProperties/@agentId}" ID="{$jobProperties/@ID}" LSIDateTime="{$jobProperties/@LSIDateTime}"> {
                                     for $liveStateInfo in $liveStateInfosSorted/state-types:LiveStateInfo
                                     let $lSIDateTime := density:stringToDateTime($liveStateInfo/@LSIDateTime)
                                     let $lSIDateTimeNext := $liveStateInfo/following-sibling::state-types:LiveStateInfo[1]/@LSIDateTime
                                     let $lSIDateTimeNextValue := if(exists($lSIDateTimeNext)) 
                                                                  then 
                                                                    density:stringToDateTime($lSIDateTimeNext) 
                                                                  else 
                                                                    if (compare( xs:string("FINISHED") , $liveStateInfo/state-types:StateName/text()) eq 0) 
                                                                    then $lSIDateTime 
                                                                    else current-dateTime() 
                                     where
                                       (($startDateTime < $lSIDateTime) and ( $endDateTime > $lSIDateTime )) or 
                                       (($startDateTime > $lSIDateTime) and ($startDateTime < $lSIDateTimeNextValue)) 
                                     return <state-types:LiveStateInfo LSIDateTime="{$lSIDateTime}" LSIDateTimeEnd="{$lSIDateTimeNextValue}">
                                              { $liveStateInfo/* }
                                            </state-types:LiveStateInfo>
                                    } </rep:group>
									
              (: where  $jobProperties/@LSIDateTime!="" :)
              return  $fish
              
  return <focused>{ $sonuc } </focused>
} ;

declare function density:SSSInterval($documentUrl as xs:string, $startDateTime as xs:dateTime, $endDateTime as xs:dateTime, $reportParameters as element(rep:reportParameters) ) as node()
{
  let $focused := density:focusedRecords($documentUrl, $startDateTime, $endDateTime, $reportParameters)
  let $tektek :=
     let $sonuc := $focused/rep:group
     return <rep:data sDTime="{$startDateTime}" eDTime="{$endDateTime}"> { $sonuc } </rep:data>
  return $tektek

} ;

declare function density:calcStat($documentUrl as xs:string, $startDateTime as xs:dateTime, $endDateTime as xs:dateTime, $reportParameters as element(rep:reportParameters) ) as node()
{
let $focused := density:SSSInterval($documentUrl, $startDateTime, $endDateTime, $reportParameters)

  let $stateRelatedA1 := $reportParameters/rep:stateRelatedA1
  let $liveStateInfo  := $stateRelatedA1/state-types:LiveStateInfo[1] (: //TODO Simdilik bir taneye bakacagiz, birden fazla olma durumunu dusunecegiz. :)
  let $stateName      := $liveStateInfo/state-types:StateName/text()
  let $substateName   := $liveStateInfo/state-types:SubstateName/text()
  let $statusName     := $liveStateInfo/state-types:StatusName/text()
  
let $tektek :=
  let $sonuc := for $liveStateInfos in $focused/rep:group
                 let $lsi := 
                    for $liveStateInfo in $liveStateInfos/state-types:LiveStateInfo
                    let $lSIDateTime := $liveStateInfo/@LSIDateTime
                    let $lSIDateTimeEnd := $liveStateInfo/@LSIDateTimeEnd
                    let $rightState := if(compare($stateName, $liveStateInfo/state-types:StateName/text()) eq 0) then true() else false()
                    let $rightSubstate := if(compare($substateName, $liveStateInfo/state-types:SubstateName/text()) eq 0) then true() else false()
                    let $rightStatus := if(compare($statusName, $liveStateInfo/state-types:StatusName/text()) eq 0) then true() else false()
                    let $finishedState := $liveStateInfo/following-sibling::state-types:LiveStateInfo[state-types:StateName eq xs:string("FINISHED")]
                    let $finishedValue := if(exists($finishedState)) 
                                                                  then 
                                                                    true()
                                                                  else 
                                                                    false() 
                    where (
                            (($startDateTime < $lSIDateTime) and ( $endDateTime > $lSIDateTime )) or 
                            (($startDateTime > $lSIDateTime) and ($startDateTime < $lSIDateTimeEnd))
                           )
                           and $rightState and $rightSubstate and $rightStatus
                    return <a>{ $liveStateInfo, <finish>{ $finishedValue } </finish> }</a>
                           
              return if(exists($lsi)) then <rep:group agentId="{$liveStateInfos/@agentId}" ID="{$liveStateInfos/@ID}" LSIDateTime="{$lsi/state-types:LiveStateInfo/@LSIDateTime}" LSIDateTimeEnd="{$lsi/state-types:LiveStateInfo/@LSIDateTimeEnd}" isFinished="{data($lsi/finish)}"/>
                     else ()
 
  return <rep:data sDTime="{$startDateTime}" eDTime="{$endDateTime}" count="{count($sonuc)}"> { $sonuc } </rep:data>
return $tektek

} ;

declare function density:recStat($documentUrl as xs:string, $reportParameters as element(rep:reportParameters) ) as node()
{
  (: Otomatik zaman penceresi hesabi icin :)
      
  let $arithmeticA  := $reportParameters/rep:arithmeticA
  let $isCumulative := $arithmeticA/@isCumulative

  let $historyA  := $reportParameters/rep:historyA
  let $numberOfRuns := $historyA/@numberOfRuns

  let $setA  := $reportParameters/rep:setA
  let $jobId := $setA/@jobId
  let $scenarioId := $setA/@scenarioId
  let $runId := $setA/@runId
  let $justFirstLevel := $setA/@justFirstLevel
  let $maxNumOfListedJobs := $setA/@maxNumOfListedJobs
  let $docId := $setA/@docId
  let $isGlobal := $setA/@isGlobal
  let $countInstancesAsOne := $setA/@countInstancesAsOne

  let $sortingA  := $reportParameters/rep:sortingA
  let $orderBy := $sortingA/@orderBy
  let $order := $sortingA/@order

  let $stateRelatedA2 := $reportParameters/rep:stateRelatedA2
  let $stateFilter  := $stateRelatedA2/@stateFilter

  let $statisticsA := $reportParameters/rep:statisticsA
  let $statSampleNumber  := $statisticsA/@statSampleNumber

  let $timeRelatedA1 := $reportParameters/rep:timeRelatedA1
  let $startDateTime := xs:dateTime($timeRelatedA1/@startDateTime)
  let $endDateTime   := xs:dateTime($timeRelatedA1/@endDateTime)
  let $typeOfTime    := $timeRelatedA1/@typeOfTime
  let $timeZone      := $timeRelatedA1/@timeZone
  let $automaticTimeInterval := $timeRelatedA1/@automaticTimeInterval

  let $timeRelatedA2 := $reportParameters/rep:timeRelatedA2
  let $step := hs:get-dayTimeDuration-from-dateTimes( adjust-dateTime-to-timezone( xs:dateTime($timeRelatedA2/@stepForDensity ), xs:dayTimeDuration('-PT0H')))
  let $maxNumberOfInterval := $timeRelatedA2/@maxNumberOfIntervals

  let $userA := $reportParameters/rep:userA
  let $userId := $userA/@userId
  let $role := $userA/@role

  
  let $hepsi := hs:getJobArray( hs:getJobsReport($documentUrl, $reportParameters), $reportParameters)
  let $startDateTimex := xs:dateTime(if($hepsi/@overallStart eq '') then current-dateTime() else $hepsi/@overallStart)-xs:dayTimeDuration('PT10S')
  let $endDateTimex := xs:dateTime(if($hepsi/@overallStop  eq '') then current-dateTime() else $hepsi/@overallStop)+xs:dayTimeDuration('PT10S')

  (:
    let $startDateTime := xs:dateTime("2013-05-05T15:52:00+03:00")
    let $endDateTime   := xs:dateTime("2013-05-05T16:54:00+03:00")
  :)
  let $sonucx :=
   if($endDateTimex > $startDateTimex) then
    let $diff := $endDateTimex - $startDateTimex
    let $numberOfIntervalCalc := xs:integer($diff div $step)
    
    let $stepCalc := 
      if($numberOfIntervalCalc > $maxNumberOfInterval) 
      then 
       (: Aralik cok buyukse step kucuk oldugunda hesaplama cok uzun surer. Bunun icin optimizasyon yapiyoruz.:)
        hs:total-duration-from-seconds( hs:total-seconds-from-duration($diff) div $maxNumberOfInterval )
      else 
        $step
        
    let $numberOfInterval := 
      if($numberOfIntervalCalc > $maxNumberOfInterval) 
      then 
       (: Aralik cok buyukse step kucuk oldugunda hesaplama cok uzun surer. Bunun icin optimizasyon yapiyoruz.:)
        xs:integer($diff div $stepCalc)
      else 
        $numberOfIntervalCalc

    let $sonuc :=
      let $seq := 1 to $numberOfInterval
      for $n in $seq
       let $kac := xs:integer($n)-1
       let $startDTime := $startDateTimex+ $kac*$stepCalc 
       let $endDTime := $startDateTimex+ $n*$stepCalc
       let $fonk := density:calcStat($documentUrl, $startDTime, $endDTime, $reportParameters)
      return $fonk      
    return <rep:statistics xmlns:rep="http://www.likyateknoloji.com/XML_report_types" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> { $sonuc } </rep:statistics>
   else ()

   return $sonucx
};

(:
let $startDateTime := xs:dateTime("2013-05-05T15:42:00+03:00")
let $endDateTime   := xs:dateTime("2013-05-05T15:59:00+03:00")

return  density:recStat($documentUrl, xs:string("RUNNING"), xs:string("ON-RESOURCE"), xs:string("TIME-IN") , $startDateTime, $endDateTime, xs:dayTimeDuration('PT60S'))

:)
(:   density:recStat(<rep:statistics></rep:statistics>, $startDateTime, $endDateTime, xs:dayTimeDuration('PT60S')) :)

(: let $endDTime := $endDateTime  $startDateTime + xs:dayTimeDuration('PT30S') :)
(: let $sonson := density:recStat(<rep:statistics></rep:statistics>, $startDateTime, $endDateTime) :)
(: return <rep:statistics state="{xs:string("FINISHED")}" substate="{xs:string("COMPLATED")}" status="{xs:string("FAILED")}"> { $sonson/* } </rep:statistics> :)
(:   :let $focused := density:SSSInterval($startDateTime, $endDTime):)
(:   :let $focused := density:calcStat(xs:string("RUNNING"), xs:string("ON-RESOURCE"), xs:string("TIME-IN"), $startDateTime, $endDTime) :)
(:   density:focusedRecords(xs:string("FINISHED"), xs:string("COMPLETED"), xs:string("FAILED"), $startDateTime, $endDTime) :)