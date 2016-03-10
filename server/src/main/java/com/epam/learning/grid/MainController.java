package com.epam.learning.grid;

import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.Collection;

@Path("/")
@Produces("application/json")
@Controller
public class MainController {

    @Autowired
    private Ignite ignite;

    @GET
    public Collection<Integer> main() {
        Collection<IgniteCallable<Integer>> calls = new ArrayList<>();

        // Iterate through all words in the sentence and create callable jobs.
        for (final String word : "Count characters using callable".split(" ")) {
            calls.add((IgniteCallable<Integer>) () -> {
                System.out.println();
                System.out.println(">>> Printing '" + word + "' on this node from ignite job.");

                return word.length();
            });
        }

        // Execute collection of callables on the ignite.
        return ignite.compute().call(calls);
    }

}
