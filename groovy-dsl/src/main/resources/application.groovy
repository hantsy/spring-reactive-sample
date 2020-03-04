import com.example.demo.DataInitializer
import com.example.demo.PostHandler
import com.example.demo.PostRepository

beans {

    postHandler(PostHandler){
        posts = ref("postRepository")
    }

    dataInitializer(DataInitializer){
        posts = ref("postRepository")
    }

    postRepository(PostRepository){
        template = ref("reactiveMongoTemplate")
    }

}
