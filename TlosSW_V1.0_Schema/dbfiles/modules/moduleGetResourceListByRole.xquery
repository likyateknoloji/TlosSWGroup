xquery version "1.0";

module namespace hs = "http://hs.tlos.com/";

import module namespace met = "http://meta.tlos.com/" at "moduleMetaDataOperations.xquery";

declare namespace usr = "http://www.likyateknoloji.com/XML_user_types";
declare namespace per = "http://www.likyateknoloji.com/XML_permission_types";
declare namespace com = "http://www.likyateknoloji.com/XML_common_types";
declare namespace out = "http://www.likyateknoloji.com/XML_userOutput_types";

(:
Mappings
$userDocumentUrl = doc('xmldb:exist:///db/TLOSSW/xmls/tlosSWUser10.xml')
$permissionDocumentUrl = doc('xmldb:exist:///db/TLOSSW/xmls/tlosSWPermission10.xml')
:)

declare function hs:query_username($documentUrl as xs:string, $in_username as xs:string) as element(out:UserResourceMap)* 
{
    let $userDocumentUrl := met:getMetaData($documentUrl, "user")
	let $permissionDocumentUrl := met:getMetaData($documentUrl, "permissions")
	
    for $person in doc($userDocumentUrl)/usr:user-infos/usr:userList/usr:person 
    where $person/com:userName/text() = $in_username 
    return 
    <out:UserResourceMap xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
 	 xmlns:out="http://www.likyateknoloji.com/XML_userOutput_types" 
	 xmlns:usr="http://www.likyateknoloji.com/XML_user_types" 
	 xmlns:com="http://www.likyateknoloji.com/XML_common_types" 
	 xmlns:per="http://www.likyateknoloji.com/XML_permission_types">
	 {
	  $person
	 }
	 <out:resources>
			{
		for $permission in doc($permissionDocumentUrl)/per:permissions/per:permission 
		where $person/com:role = $permission/per:roles/per:role 
		order by $person/com:role 
		return 
			$permission/per:resource
			}
	 </out:resources>
    </out:UserResourceMap>
};
