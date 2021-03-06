/* order_test_1Test.java

	Purpose:
		
	Description:
		
	History:
		Sep, 7, 2010 17:30:59 PM

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

This program is distributed under Apache License Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/

//select F6:I9, and CTRL+X
public class SS_155_Test extends SSAbstractTestCase {
	
	/**
	 * Select cells and use Ctrl + 'x'
	 */
	@Override
	protected void executeTest() {
		//verify
		verifyTrue(jq("div.zshighlight").width() == 0);

		selectCells(5,5,8,8);		
		pressCtrlWithChar(X);
		
		/**
		 * Expect:
		 * 
		 * select range has high light
		 */
		verifyTrue(jq("div.zshighlight").width() != 0);
	}
}

