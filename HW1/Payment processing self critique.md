1. What is the architecture type used in your solution?
2. Are there any areas you feel missing in the architecture? Is the architecture complete?
3. Do you think the system architecture is an open architecture? Does it allow you to add more components easily?




My groups proposed architecture, we idealized a 3 tiered architecture. I was thinking that this would be a good starting point for a distributed system. I was not thinking this would be more than 100 requests per second. Along side this, I felt that the architecture would scale better if I addressed these missing components, such as a load balancer into a compute cluster, and a distributed database for storing payment information. 

I felt that this is an open architecture as it allows for addition for new components such as a load balancer and distributed cluter compute for 