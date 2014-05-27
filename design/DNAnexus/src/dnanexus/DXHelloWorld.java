/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package dnanexus;

import java.io.IOException;

import com.dnanexus.DXJSON;
import com.dnanexus.DXUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class DXHelloWorld {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HelloWorldInput {
        @JsonProperty
        private String name;
    }

    private static class HelloWorldOutput {
        @JsonProperty
        private String greeting;

        public HelloWorldOutput(String greeting) {
            this.greeting = greeting;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("This is the DNAnexus Java Demo App");

        System.out.println("A");
        HelloWorldInput input = DXUtil.getJobInput(HelloWorldInput.class);

         System.out.println("B");
        String name = input.name;
        String greeting = "Hello, " + (name == null ? "World" : name) + "!";

         System.out.println("C");
        DXUtil.writeJobOutput(new HelloWorldOutput(greeting));
         System.out.println("D");
    }

}
