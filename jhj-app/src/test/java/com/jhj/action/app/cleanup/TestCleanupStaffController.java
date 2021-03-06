package com.jhj.action.app.cleanup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jhj.action.app.JUnitActionBase;

public class TestCleanupStaffController extends JUnitActionBase{
	
	//重建用户的充值记录及消费明细，生成余额
	@Test
    public void testCleanupStaff() throws Exception {

		String url = "/app/job/cleanup/staff.json";
		MockHttpServletRequestBuilder getRequest = get(url);

	  
	    ResultActions resultActions = mockMvc.perform(getRequest);

	    resultActions.andExpect(content().contentType(this.mediaType));
	    resultActions.andExpect(status().isOk());

	    System.out.println("RestultActions: " + resultActions.andReturn().getResponse().getContentAsString());
    }

}
